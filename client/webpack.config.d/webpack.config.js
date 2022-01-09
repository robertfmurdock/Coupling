const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackHarddiskPlugin = require('html-webpack-harddisk-plugin');
const ScriptExtHtmlWebpackPlugin = require('script-ext-html-webpack-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const resourcesPath = path.resolve(__dirname, '../../../../client/build/processedResources/js/main');

if (config.entry && config.entry.main) {
    config.entry.main = [path.resolve(resourcesPath, "com/zegreatrob/coupling/client/app.js")].concat(config.entry.main);
}

if (config.output) {
    config.output.publicPath = '/app/build/'
}
if(!config.resolve.modules) {
    config.resolve.modules = []
}
config.resolve.modules.push(resourcesPath);
let nodeModules = path.resolve(__dirname, '../../../../build/js/node_modules');
config.resolve.modules.push(nodeModules);
config.resolve.fallback = { "assert": false };
config.module.rules.push(
    {
        test: /\.(md|graphql)$/,
        use: 'raw-loader',
        include: resourcesPath,
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
        type: 'asset'
    }
);
config.externals = {"cheerio": "window", "fs": "empty"}

if (config.devServer) {
    config.devServer.port = 3001
    config.devServer.hot = true
    config.devServer.historyApiFallback = {index: 'index.html'}
    let distributionPath = path.resolve(__dirname, '../../../../client/build/distributions');
    config.devServer.static.push({
        directory : distributionPath,
        publicPath : '/app/build',
    })
    config.devServer.static.push({
        directory : distributionPath,
        publicPath : '/',
    })
}

config.cache = true

config.plugins.push(
    new HtmlWebpackPlugin({
        alwaysWriteToDisk: true,
        title: 'Coupling Dev Server',
        file: "index.html",
        scriptLoading: 'defer',
        template: path.resolve(resourcesPath, 'template.html'),
        devServer: config.devServer ? config.devServer.port : undefined,
        appMountClass: 'view-container',
        window: config.devServer ? {
            isAuthenticated: true,
            expressEnv: "dev",
            inMemory: true,
        } : {}
    }),
    new ScriptExtHtmlWebpackPlugin({
        defaultAttribute: 'async'
    }),
    new HtmlWebpackHarddiskPlugin(),
    new FaviconsWebpackPlugin({
        logo: path.resolve(resourcesPath, 'images/logo.png'),
        cache: true
    }),
    new MiniCssExtractPlugin({
        filename: 'styles.css',
    }),
);
