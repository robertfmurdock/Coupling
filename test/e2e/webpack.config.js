const config = require('../../server/webpack.config');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const jsPath = path.resolve(__dirname, './');
const nodeExternals = require('webpack-node-externals');

config.entry = {
  config: path.resolve(jsPath, './protractor-conf.ts'),
  test: path.resolve(jsPath, './test.js')
};

config.output = {
  path: path.resolve(__dirname, '.tmp'),
  filename: '[name].js',
  libraryTarget: 'commonjs'
};

config.module.rules.push({
  test: /\.(css)$/,
  loader: ExtractTextPlugin.extract({
    fallback: 'style-loader',
    use: 'css-loader?' + JSON.stringify({minimize: true})
  })
});

config.module.rules.push({
  test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
  loader: 'url-loader?limit=100000'
});

config.plugins.push(new ExtractTextPlugin({filename: './styles.css', allChunks: true}));

config.target = 'node';
config.externals = [nodeExternals()];
config.mode = "development";
module.exports = config;
