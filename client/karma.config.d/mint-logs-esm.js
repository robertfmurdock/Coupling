// The TestMints browser hook requires the legacy .js name; this client emits ESM.
config.webpack.resolve = config.webpack.resolve || {}
config.webpack.resolve.alias = {
    ...config.webpack.resolve.alias,
    './testmints-mint-logs.js$': require('path').resolve(config.basePath, 'kotlin/mint-logs-esm.cjs'),
}
