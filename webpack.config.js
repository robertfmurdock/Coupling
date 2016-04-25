var path = require('path');
var BowerWebpackPlugin = require('bower-webpack-plugin');
var jsPath = path.resolve(__dirname, './public/app');

module.exports = {
  entry: path.resolve(jsPath, './app.ts'),
  output: {
    filename: path.resolve(jsPath, './main.js')
  },
  devtool: 'source-map',
  resolve: {
    root: jsPath,
    extensions: ['', '.webpack.js', '.web.js', '.ts', '.js']
  },
  module: {
    loaders: [{
      test: /\.ts$/,
      loader: 'ts-loader'
    }]
  },
  plugins: [new BowerWebpackPlugin()]
};