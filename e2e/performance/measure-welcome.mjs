import { mkdir, writeFile } from 'node:fs/promises';
import { pathToFileURL } from 'node:url';
import { join, dirname } from 'node:path';
import { execFileSync } from 'node:child_process';
import { platform, arch, release, cpus } from 'node:os';
import { installPageObservers, summarize } from './page-metrics.mjs';
import { attachNetworkTotals } from './network-totals.mjs';

const [root, reportPath, chromeBinary] = process.argv.slice(2);
const { startWebDriver } = await import(pathToFileURL(join(root, 'build/js/node_modules/@wdio/utils/build/node.js')));
const { remote } = await import(pathToFileURL(join(root, 'build/js/node_modules/webdriverio/build/node.js')));
const report = {
    schemaVersion: 2, status: 'partial',
    revision: execFileSync('git', ['rev-parse', 'HEAD'], { cwd: root, encoding: 'utf8' }).trim(),
    workingTreeDirty: execFileSync('git', ['status', '--porcelain'], { cwd: root, encoding: 'utf8' }).trim().length > 0,
    timestamp: new Date().toISOString(),
    host: { platform: platform(), arch: arch(), osRelease: release(), cpuModel: cpus()[0]?.model ?? 'unknown' },
    route: '/welcome/', auth: 'signed-out, fresh Chrome profile per pair',
    conditions: { network: 'unthrottled local app plus live external CDNs', cpu: 'unthrottled', window: [1280, 900], observationAfterReadyMs: 2000 },
    caveats: [
        'Cold means fresh browser profile; OS DNS/TLS and CDN server caches are uncontrolled.',
        'Welcome chooses randomized content; content variation is not controlled by this baseline.',
        'A supported observer with no LCP entry is reported as null, not zero.',
        'Resource Timing bytes exclude sizes hidden by Timing-Allow-Origin; network.reportedBytesReceived is a separate browser-protocol counter, not a wire capture.',
        'Network totals include only responses to requests started in the sample and completed before its snapshot. Pending and failed requests are separate.',
        'LCP and CLS are bounded lab observations, not field Core Web Vitals. No INP is measured.',
        'Readiness is the visible Come on in! button. No authenticated data is loaded or changed.',
    ],
    samples: [],
};

async function measure(browser, network, cache, pair) {
    network.reset();
    const start = performance.now();
    if (cache === 'warm-reload') await browser.refresh();
    else await browser.url('https://localhost/welcome/');
    const enter = await browser.$('button=Come on in!');
    await enter.waitForDisplayed({ timeout: 60000 });
    const readinessElapsedMs = performance.now() - start;
    const readyAtMs = await browser.execute(() => performance.now());
    await browser.waitUntil(async () => await browser.execute(() => performance.now()) >= readyAtMs + 2000, { timeout: 10000, interval: 100 });
    const snapshot = await browser.execute(() => window.__couplingPerformanceSnapshot());
    if (!snapshot) throw new Error('Pre-navigation performance observer did not initialize');
    return { pair, cache, readinessElapsedMs, readyAtMs, ...summarize(snapshot), network: network.snapshot() };
}

await mkdir(dirname(reportPath), { recursive: true });
const saveReport = () => writeFile(reportPath, JSON.stringify(report, null, 2) + '\n');
await saveReport();

for (let pair = 1; pair <= 3; pair++) {
    let driver, browser, network;
    try {
        const options = {
            logLevel: 'error', connectionRetryCount: 0, connectionRetryTimeout: 10000,
            capabilities: {
                browserName: 'chrome', browserVersion: 'stable', acceptInsecureCerts: true,
                'goog:chromeOptions': {
                    args: ['--headless', '--force-device-scale-factor=1'],
                    ...(chromeBinary ? { binary: chromeBinary } : {}),
                },
            },
        };
        driver = await startWebDriver(options);
        browser = await remote(options);
        await browser.setWindowSize(1280, 900);
        await browser.addInitScript(installPageObservers);
        network = await attachNetworkTotals(browser);
        report.browser = { version: browser.capabilities.browserVersion, platform: browser.capabilities.platformName };
        for (const cache of ['cold-profile', 'warm-reload']) {
            const sample = await measure(browser, network, cache, pair);
            report.samples.push(sample);
            await saveReport();
            console.log(JSON.stringify({ pair, cache, readinessElapsedMs: sample.readinessElapsedMs, lcpMs: sample.lcpMs, cls: sample.cls }));
        }
    } finally {
        network?.detach();
        try {
            if (browser) await browser.deleteSession();
        } finally {
            if (driver && driver.exitCode == null && driver.signalCode == null) {
                await new Promise(resolve => { driver.once('exit', resolve); driver.kill('SIGKILL'); });
            }
        }
    }
}
report.status = 'complete';
await saveReport();
console.log(`Saved six Welcome samples to ${reportPath}`);
