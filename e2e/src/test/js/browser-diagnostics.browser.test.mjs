import {test} from 'node:test'
import assert from 'node:assert/strict'
import {createServer} from 'node:http'
import {readFile, stat} from 'node:fs/promises'
import path from 'node:path'
import {randomUUID} from 'node:crypto'
import {remote} from '../../../../build/js/node_modules/webdriverio/build/node.js'
import {createCollector} from '../../../diagnostics/browser-diagnostics.mjs'

test('real Chrome preserves pre-bootstrap script, console, and network failures in an artifact', {timeout: 60000}, async t => {
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
    t.after(() => new Promise(resolve => server.close(resolve)))
    const browser = await remote({logLevel: 'error', capabilities: {
        browserName: 'chrome', browserVersion: 'stable', acceptInsecureCerts: true,
        'goog:chromeOptions': {args: ['--headless'], ...(process.env.WDIO_CHROME_BINARY ? {binary: process.env.WDIO_CHROME_BINARY} : {})},
    }})
    const directory = path.resolve('build/reports/browser-diagnostics-fixture', randomUUID())
    const collector = createCollector(browser, {directory, identity: () => ({suite: 'diagnostic-fixture', testId: 'bootstrap'})})
    t.after(async () => {
        try { await collector.stop() }
        finally { await browser.deleteSession() }
    })
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
