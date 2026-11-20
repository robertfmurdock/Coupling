import {createRequire} from 'node:module';

// The Kotlin main module contains policy only: importing it registers no tests.
export function configure(config) {
    if (!process.env.COUPLING_CAPTURE_DIRECTORY) return config;
    const {CaptureLifecycle} = createRequire(import.meta.url)(process.env.COUPLING_CAPTURE_POLICY_MODULE)
        .com.zegreatrob.coupling.e2e.visual;
    const prepare = config.onPrepare;
    const complete = config.onComplete;
    config.mochaOpts = {...config.mochaOpts, fgrep: undefined, timeout: 120000};
    config.onPrepare = async (...args) => {
        process.env.COUPLING_CAPTURE_MANIFEST = await CaptureLifecycle.begin();
        console.log(`Visual evidence manifest: ${process.env.COUPLING_CAPTURE_MANIFEST}`);
        if (prepare) await prepare(...args);
    };
    config.onComplete = async (exitCode, ...args) => {
        try {
            if (complete) await complete(exitCode, ...args);
        } catch (error) {
            await CaptureLifecycle.finish(process.env.COUPLING_CAPTURE_MANIFEST, 1);
            throw error;
        }
        if (!await CaptureLifecycle.finish(process.env.COUPLING_CAPTURE_MANIFEST, exitCode)) {
            throw new Error('Visual evidence did not complete; inspect manifest');
        }
    };
    return config;
}
