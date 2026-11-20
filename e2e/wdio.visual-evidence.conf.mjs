import {createRequire} from 'node:module';

const {config: baseConfig} = await import(process.env.COUPLING_CAPTURE_BASE_CONFIG);
const {CaptureHooks} = createRequire(import.meta.url)(process.env.COUPLING_CAPTURE_POLICY_MODULE)
    .com.zegreatrob.coupling.e2e.visual;

export const config = CaptureHooks.configure(baseConfig);
