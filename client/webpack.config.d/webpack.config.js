const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackHarddiskPlugin = require('html-webpack-harddisk-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const DynamicCdnWebpackPlugin = require('@effortlessmotion/dynamic-cdn-webpack-plugin');
const fetch = require("node-fetch/lib/index.js")

const resourcesPath = path.resolve(__dirname, '../../../../client/build/processedResources/js/main');

if (config.entry && config.entry.main) {
    config.entry.main = [path.resolve(resourcesPath, "com/zegreatrob/coupling/client/app.js")].concat(config.entry.main);
}

if (config.output) {
    config.output.publicPath = '/app/build/'
}
if (!config.resolve.modules) {
    config.resolve.modules = []
}
config.resolve.modules.push(resourcesPath);
const nodeModules = path.resolve(__dirname, '../../../../build/js/node_modules');
config.resolve.modules.push(nodeModules);
config.resolve.fallback = {"assert": false};
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
        directory: distributionPath,
        publicPath: '/app/build',
    })
    config.devServer.static.push({
        directory: distributionPath,
        publicPath: '/',
    })
}

config.cache = true

const cdnVars = {
    "react": "React",
    "react-dom": "ReactDOM",
    "react-router": "ReactRouter",
    "react-router-dom": "ReactRouterDOM",
    "history": "HistoryLibrary",
    "blueimp-md5": "md5",
    "dom-to-image": "domtoimage"
}

const cdnFilenameCorrections = {
    "react-router": "react-router.production.min.js",
    "react-router-dom": "react-router-dom.production.min.js"
}

function lookupFileName(libName, version) {
    return cdnFilenameCorrections[libName]
        ? Promise.resolve(cdnFilenameCorrections[libName])
        : fetch(`https://api.cdnjs.com/libraries/${libName}`)
            .then((result) => result.json())
            .then(body => body.versions.includes(version) ? body.filename : null);
}

config.plugins.push(
    new HtmlWebpackPlugin({
        alwaysWriteToDisk: !!config.devServer,
        title: 'Coupling Dev Server',
        filename: "html/index.html",
        template: path.resolve(resourcesPath, 'template.html'),
        devServer: config.devServer ? config.devServer.port : undefined,
        appMountClass: 'view-container',
        inject: false,
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
        cache: true
    }),
    new MiniCssExtractPlugin({
        filename: 'styles.css',
    }),
    new DynamicCdnWebpackPlugin({
        resolver: function (libName, version) {
            if (cdnVars[libName]) {
                return lookupFileName(libName, version)
                    .then(filename => filename ? `https://cdnjs.cloudflare.com/ajax/libs/${libName}/${version}/${filename}`
                        : null)
                    .then(url => url ? {name: libName, var: cdnVars[libName] ?? libName, url, version} : null)
                    .catch(() => null)
            } else {
                return null
            }
        }
    })
);
