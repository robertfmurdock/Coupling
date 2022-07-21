const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackHarddiskPlugin = require('html-webpack-harddisk-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const resourcesPath = path.resolve(__dirname, '../../../../client/build/processedResources/js/main');
const additionalResourcesPath = path.resolve(__dirname, '../../../../client/build/additionalResources');

const fs = require('fs')

const cdnResources = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../../../../client/build/cdn.json')))
const cdnSettings = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../../../../client/cdn.settings.json')))

if (config.entry && config.entry.main) {
    config.entry.main = [path.resolve(resourcesPath, "com/zegreatrob/coupling/client/app.js")].concat(config.entry.main);
}

if (config.output) {
    config.output.publicPath = '/app/build/'
}
if (!config.resolve.modules) {
    config.resolve.modules = []
}

config.optimization = {
    splitChunks: {
        cacheGroups: {
            vendor: {
                test: /[\\/]node_modules[\\/]/,
                name: 'vendor',
                chunks: 'all',
            },
            kotlin: {
                test: /[\\/]kotlin[\\/](kotlin|korlibs)/,
                name: 'kotlin',
                chunks: 'all',
            },
            ktor: {
                test: /[\\/]kotlin[\\/]ktor/,
                name: 'ktor',
                chunks: 'all',
            },
        },
    },
}

const nodeModules = path.resolve(__dirname, '../../../../build/js/node_modules');
config.resolve.modules.push(resourcesPath, additionalResourcesPath, nodeModules);
config.resolve.fallback = {"assert": false};
config.module.rules.push(
    {
        test: /\.(md|graphql)$/,
        use: 'raw-loader',
        include: [resourcesPath, additionalResourcesPath],
    },
    {
        test: /\.(sa|sc|c)ss$/,
        include: [resourcesPath, nodeModules],
        use: [
            {
                loader: MiniCssExtractPlugin.loader,
                options: {
                    publicPath: ''
                }
            },
            {
                loader: 'css-loader',
                options: {
                    modules: 'global'
                }
            },
        ],
    }, {
        test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
        include: [resourcesPath, nodeModules],
        type: 'asset',
        generator: {
            filename: 'html/[hash][ext][query]'
        }
    }
);
config.performance = {
    assetFilter: function (assetFilename) {
        return assetFilename.endsWith('.js') || assetFilename.endsWith('.css');
    },
}

config.externals = {"cheerio": "window", "fs": "empty", ...cdnSettings}

if (config.devServer) {
    config.devServer.port = 3001
    config.devServer.hot = true
    config.devServer.historyApiFallback = {index: 'html/index.html'}
    let distributionPath = path.resolve(__dirname, '../../../../client/build/distributions');
    config.devServer.static.push({
        directory: distributionPath,
        publicPath: '/app/build',
    })
    config.devServer.static.push({
        directory: distributionPath,
        publicPath: '/',
    })
}

config.cache = true

config.plugins.push(
    new HtmlWebpackPlugin({
        alwaysWriteToDisk: !!config.devServer,
        title: 'Coupling Dev Server',
        filename: "html/index.html",
        template: path.resolve(resourcesPath, 'template.html'),
        devServer: config.devServer ? config.devServer.port : undefined,
        appMountClass: 'view-container',
        inject: false,
        cdnContent: Object.values(cdnResources),
        window: config.devServer ? {
            expressEnv: "dev",
            inMemory: true,
            auth0ClientId: "rchtRQh3yX5akg1xHMq7OomWyXBhJOYg",
            auth0Domain: "zegreatrob.us.auth0.com",
            basename: '',
            prereleaseMode: true,
        } : {}
    }),
    new HtmlWebpackHarddiskPlugin(),
    new FaviconsWebpackPlugin({
        logo: path.resolve(resourcesPath, 'images/logo.png'),
        cache: true,
        prefix: 'html/assets/',
    }),
    new MiniCssExtractPlugin({
        filename: 'html/styles.css',
    }),
);
