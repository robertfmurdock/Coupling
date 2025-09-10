const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackHarddiskPlugin = require('html-webpack-harddisk-plugin');
const WebpackFavicons = require('webpack-favicons');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const resourcesPath = path.resolve(__dirname, '../../../../client/build/processedResources/js/main');
const additionalResourcesPath = path.resolve(__dirname, '../../../../client/build/additionalResources');

const fs = require('fs')

let cdnFile = fs.readFileSync(path.resolve(__dirname, '../../../../client/build/cdn.json'), {encoding: "UTF-8"});
if (config.devServer) {
    cdnFile = cdnFile.replaceAll('production', 'development')
}

const cdnResources = JSON.parse(
    cdnFile
        .split(/\r?\n/)
        .filter((line) => !line.includes("TRACE"))
        .join("\n")
)

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
        test: /\.(?:js|mjs|cjs|jsx)$/,
        include: [resourcesPath],
        use: {
            loader: 'babel-loader',
            options: {
                presets: [
                    ['@babel/preset-react', {targets: "defaults"}]
                ]
            }
        }
    },
    {
        test: /\.(sa|sc|c)ss$/,
        include: [resourcesPath, nodeModules],
        use: [
            {
                loader: MiniCssExtractPlugin.loader,
                options: {
                    publicPath: '',
                    defaultExport: true,
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
    let distributionPath = path.resolve(__dirname, 'dist');
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
    new MiniCssExtractPlugin({
        filename: 'html/styles.css',
    }),
);
if (!config.devServer) {
    config.plugins.push(
        new WebpackFavicons({
            src: path.resolve(resourcesPath, 'images/logo.png'),
            path: 'html/assets/',
            icons: {
                favicons: true
            }
        }),
    )
}
