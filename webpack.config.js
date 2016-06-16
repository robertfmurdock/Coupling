var webpack = require('webpack');
var path = require('path');
var BowerWebpackPlugin = require('bower-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var jsPath = path.resolve(__dirname, './public/app');


console.log('Packing for ', process.env.NODE_ENV);

var exports = {
  entry: path.resolve(jsPath, './app.ts'),
  output: {
    path: './public/app/build',
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
        test: /\.css$/,
        loader: ExtractTextPlugin.extract('style-loader', 'css-loader')
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=100000'
      }
    ]
  },
  plugins: [
    new BowerWebpackPlugin(),
    new ExtractTextPlugin('./styles.css')
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