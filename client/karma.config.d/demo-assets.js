// Match Vite's root-relative image imports using Gradle's processed resources.
config.webpack.resolve = config.webpack.resolve || {}
config.webpack.resolve.alias = {
    ...config.webpack.resolve.alias,
    '/images': require('path').resolve(config.basePath, 'kotlin/images'),
}
config.webpack.module.rules.push({
    test: /\.(png|svg)$/i,
    type: 'asset/inline',
})
