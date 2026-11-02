import {test} from 'node:test'
import assert from 'node:assert/strict'
import {createServer} from 'node:http'
import {readFile, stat} from 'node:fs/promises'
import path from 'node:path'
import {randomUUID} from 'node:crypto'
import {remote} from '../../../../build/js/node_modules/webdriverio/build/node.js'
import {createCollector} from '../../../diagnostics/browser-diagnostics.mjs'
import {startWebDriver} from '../../../../build/js/node_modules/@wdio/utils/build/node.js'
import {openFixtureSession} from './browser-fixture-session.mjs'

function openBrowser(t) {
    return openFixtureSession({logLevel: 'error', connectionRetryCount: 0, connectionRetryTimeout: 10000, capabilities: {
        browserName: 'chrome', browserVersion: 'stable', acceptInsecureCerts: true,
        'goog:chromeOptions': {args: ['--headless'], ...(process.env.WDIO_CHROME_BINARY ? {binary: process.env.WDIO_CHROME_BINARY} : {})},
    }}, {startDriver: startWebDriver, connect: remote, after: cleanup => t.after(cleanup)})
}

test('real Chrome preserves pre-bootstrap script, console, and network failures in an artifact', {timeout: 60000}, async t => {
    let collector
    t.after(() => collector?.stop())
    const server = createServer((request, response) => {
        const pathname = new URL(request.url, 'http://localhost').pathname
        if (pathname === '/reset') { request.socket.destroy(); return }
        if (pathname === '/error') { response.writeHead(503); response.end('unavailable'); return }
        if (pathname === '/favicon.ico') { response.writeHead(204); response.end(); return }
        response.setHeader('Content-Type', 'text/html')
        response.end(`<div class="view-container"></div><script>
            console.warn('fixture warning'); console.error('fixture console error');
            setTimeout(() => { throw new Error('fixture uncaught exception') }, 0);
            Promise.all([
                fetch('/reset?token=fixture-secret').catch(() => {}),
                fetch('/error?token=fixture-secret')
            ]).then(() => { window.fixtureFinished = true });
        </script>`)
    })
    await new Promise(resolve => server.listen(0, '127.0.0.1', resolve))
    t.after(() => { server.closeAllConnections(); return new Promise(resolve => server.close(resolve)) })
    const browser = await openBrowser(t)
    const directory = path.resolve('build/reports/browser-diagnostics-fixture', randomUUID())
    collector = createCollector(browser, {directory, identity: () => ({suite: 'diagnostic-fixture', testId: 'bootstrap'})})
    await collector.start()
    await browser.url(`http://127.0.0.1:${server.address().port}/`)
    await browser.waitUntil(() => browser.execute(() => window.fixtureFinished === true), {timeout: 5000})
    const primary = new Error('deliberate bootstrap failure')
    collector.markBootstrapFailure(primary)
    const filename = await collector.captureFailure(primary)
    assert.equal(await collector.captureFailure(new Error('cached bootstrap failure')), filename)
    const reportText = await readFile(filename, 'utf8')
    const report = JSON.parse(reportText)
    assert.ok(report.events.some(entry => entry.message?.includes('fixture warning') && entry.level === 'WARN'))
    assert.ok(report.events.some(entry => entry.message?.includes('fixture console error') && entry.level === 'ERROR'))
    assert.ok(report.events.some(entry => entry.kind === 'javascript' && entry.message?.includes('fixture uncaught exception')))
    assert.ok(report.events.some(entry => entry.source === 'network' && entry.status === 503))
    assert.ok(report.events.some(entry => entry.source === 'network' && entry.error))
    assert.equal(report.snapshot.mountChildren, 0)
    assert.ok((await stat(path.join(directory, report.screenshot))).size > 0)
    assert.equal(reportText.includes('fixture-secret'), false)
    assert.deepEqual(report.captureErrors, [])
    console.log(`Browser diagnostic fixture artifact: ${filename}`)
})

test('maintained mobile polyfill delivers touch dragenter and drop without browser warnings', {timeout: 60000}, async t => {
    let collector
    t.after(() => collector?.stop())
    const polyfill = await readFile(path.resolve('../build/js/node_modules/mobile-drag-drop/index.min.js'))
    const server = createServer((request, response) => {
        if (request.url === '/polyfill.js') { response.setHeader('Content-Type', 'application/javascript'); response.end(polyfill); return }
        if (request.url === '/favicon.ico') { response.writeHead(204); response.end(); return }
        response.setHeader('Content-Type', 'text/html')
        response.end(`<style>body{touch-action:none}#source,#target{position:absolute;top:50px;width:80px;height:80px;background:#ddd}#source{left:50px}#target{left:250px}</style>
            <div id="source" draggable="true">source</div><div id="target">target</div><script src="/polyfill.js"></script><script>
            MobileDragDrop.polyfill({forceApply:true});
            document.querySelector('#source').addEventListener('dragstart', e => e.dataTransfer.setData('text/plain','payload'));
            document.querySelector('#target').addEventListener('dragenter', e => { e.preventDefault(); window.entered = true });
            document.querySelector('#target').addEventListener('dragover', e => e.preventDefault());
            document.querySelector('#target').addEventListener('drop', e => { e.preventDefault(); window.dropped = e.dataTransfer.getData('text/plain') });
            </script>`)
    })
    await new Promise(resolve => server.listen(0, '127.0.0.1', resolve))
    t.after(() => { server.closeAllConnections(); return new Promise(resolve => server.close(resolve)) })
    const browser = await openBrowser(t)
    collector = createCollector(browser)
    await collector.start()
    await browser.url(`http://127.0.0.1:${server.address().port}/`)
    await browser.performActions([{type: 'pointer', id: 'touch', parameters: {pointerType: 'touch'}, actions: [
        {type: 'pointerMove', duration: 0, x: 90, y: 90}, {type: 'pointerDown', button: 0},
        {type: 'pointerMove', duration: 200, x: 110, y: 90}, {type: 'pause', duration: 200},
        {type: 'pointerMove', duration: 300, x: 290, y: 90}, {type: 'pause', duration: 200}, {type: 'pointerUp', button: 0},
    ]}])
    await browser.releaseActions()
    await browser.waitUntil(() => browser.execute(() => window.dropped === 'payload'), {timeout: 2000})
    assert.equal(await browser.execute(() => window.entered), true)
    assert.deepEqual((await collector.drainLogs()).filter(entry => ['WARN', 'ERROR'].includes(entry.level)), [])
})
