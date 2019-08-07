const clone = require('ramda/src/clone');
const webpack = require('webpack');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpackConfig = clone(require('../webpack.config'));
const path = require('path');

webpackConfig.externals.jquery = 'jQuery';

function testResolve() {
  let resolve = clone(webpackConfig.resolve);
  resolve.modules = [
    path.resolve(__dirname, '../build/node_modules_imported'),
    path.resolve(__dirname, '../build/classes/kotlin/main'),
    path.resolve(__dirname, '../build/resources/main'),
    path.resolve(__dirname, '../build/classes/kotlin/test'),
    path.resolve(__dirname, '../node_modules'),
    path.resolve(__dirname, '../app')
  ];

  return resolve;
}

const SpeedMeasurePlugin = require("speed-measure-webpack-plugin");

const smp = new SpeedMeasurePlugin();

const config = {
  module: webpackConfig.module,
  entry: path.resolve(__dirname, 'tests.bundle.js'),
  resolve: testResolve(),
  externals: webpackConfig.externals,
  mode: "development",
  plugins: [
    new MiniCssExtractPlugin({
      filename: './styles.css'
    }),
    new webpack.DllReferencePlugin({
      context: '.',
      manifest: require('../build/lib/test-vendor/testVendor-manifest.json')
    }),
    new webpack.ProvidePlugin({'window.jQuery': 'jquery', $: 'jquery', 'jQuery': 'jquery'})
  ],
  optimization: {
    removeAvailableModules: false,
    removeEmptyChunks: false,
    splitChunks: false,
  },
  devtool: 'inline-source-map'
};

module.exports = config;
