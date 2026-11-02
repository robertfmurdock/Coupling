import {once} from 'node:events'

// Own the driver before requesting a session: remote() cannot return a browser
// for cleanup when Chrome crashes during newSession.
export async function openFixtureSession(options, {startDriver, connect, after}) {
    const driver = await startDriver(options)
    let stopped = false
    async function stopDriver() {
        if (stopped) return
        stopped = true
        if (!driver || driver.exitCode !== null || driver.signalCode !== null) return
        const exited = once(driver, 'exit')
        driver.kill('SIGKILL')
        await exited
    }
    let browser
    after?.(async () => {
        try { if (browser) await browser.deleteSession() }
        finally { await stopDriver() }
    })
    try {
        browser = await connect(options)
        return browser
    } catch (primary) {
        try { await stopDriver() }
        catch (cleanup) { primary.cause ??= cleanup }
        throw primary
    }
}
