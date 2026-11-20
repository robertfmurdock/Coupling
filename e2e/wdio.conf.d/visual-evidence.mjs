export const configure = process.env.COUPLING_CAPTURE_HOOK
    ? (await import(process.env.COUPLING_CAPTURE_HOOK)).configure
    : config => config;
