var path = require('path');
var BowerWebpackPlugin = require('bower-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var jsPath = path.resolve(__dirname, './public/app');

module.exports = {
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