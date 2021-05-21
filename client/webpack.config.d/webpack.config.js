const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HtmlWebpackHarddiskPlugin = require('html-webpack-harddisk-plugin');
const ScriptExtHtmlWebpackPlugin = require('script-ext-html-webpack-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin')

let resourcesPath = path.resolve(__dirname, '../../../../client/build/processedResources/js/main');

if (config.output)
    config.output.publicPath = '/app/build/'

config.resolve.modules.push(resourcesPath);
config.resolve.modules.push(path.resolve(__dirname, '../../../../build/js/node_modules'));

config.module.rules.push(
    {
        test: /\.(md|graphql)$/i, use: 'raw-loader'
    },
    {
        test: /\.(sa|sc|c)ss$/,
        use: [
            {
                loader: MiniCssExtractPlugin.loader,
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
        loader: 'url-loader',
        options: {
            limit: "100000"
        }
    }
);
config.externals = {"cheerio": "window"}

if (config.devServer) {
    config.devServer.port = 3001
    // config.devServer.publicPath = '/app/build'
    config.devServer.hot = true
    config.devServer.historyApiFallback = {index: 'index.html'}
    let distributionPath = path.resolve(__dirname, '../../../../client/build/distributions');
    config.devServer.contentBase.push(distributionPath)
}

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
        filename: 'styles.css'
    }),
);
