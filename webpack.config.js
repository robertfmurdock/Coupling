var webpack = require('webpack');
var path = require('path');
var BowerWebpackPlugin = require('bower-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var jsPath = path.resolve(__dirname, './public/app');
var WebpackCleanupPlugin = require('webpack-cleanup-plugin');

console.log('Packing for ', process.env.NODE_ENV);

var exports = {
  entry: path.resolve(jsPath, './app.ts'),
  output: {
    path: path.resolve(__dirname, './public/app/build'),
    filename: 'main.js'
  },
  devtool: 'source-map',
  resolve: {
    root: jsPath,
    extensions: ['', '.webpack.js', '.web.js', '.ts', '.js']
  },
  module: {
    loaders: [
      {
        test: /\.ts$/,
        loader: 'ts-loader'
      },
      {
        test: /\.(css)$/,
        loaders: ['style-loader', 'css-loader']
      },
      {
        test: /\.(scss)$/,
        loaders: ['style-loader', 'css-loader', 'sass-loader']
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=100000'
      }
    ]
  },
  plugins: [
    new WebpackCleanupPlugin(),
    new BowerWebpackPlugin(),
    new ExtractTextPlugin('./styles.css', {allChunks: true})
  ]
};

if (process.env.NODE_ENV === 'production') {
  exports.devtool = 'cheap-module-source-map';

  exports.plugins = exports.plugins.concat([
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.UglifyJsPlugin(),
    new webpack.optimize.DedupePlugin(),
    new webpack.optimize.OccurrenceOrderPlugin()
  ]);
}

module.exports = exports;