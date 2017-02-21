const webpack = require('webpack');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const buildPath = path.resolve(__dirname, '../public/app/build/vendor');
const config = {
  entry: {
    vendor: [
      "angular",
      "angular-animate",
      "angular-gravatar",
      "angular-mocks",
      "angular-native-dragdrop",
      "angular-resource",
      "angular-route",
      "d3-color",
      "d3-interpolate",
      "d3-selection",
      "ramda/src/map",
      "font-awesome/css/font-awesome.css",
      "underscore",
    ]
  },
  output: {
    path: buildPath,
    filename: '[name].js',
    library: "[name]_lib"
  },
  module: {
    loaders: [
      {
        test: /\.(css)$/,
        loader: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: 'css-loader?' + JSON.stringify({minimize: true})
        })
      },
      {
        test: /\.(scss)$/,
        loader: ExtractTextPlugin.extract({fallback: 'style-loader', use: 'css-loader!sass-loader'})
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=50000'
      }
    ]
  },
  plugins: [
    new ExtractTextPlugin({filename: '[name]-styles.css', allChunks: true}),
    new webpack.DllPlugin({
      path: path.resolve(buildPath, '[name]-manifest.json'),
      name: '[name]_lib'
    }),
    new webpack.optimize.UglifyJsPlugin(),
    new webpack.optimize.OccurrenceOrderPlugin()
  ]
};

module.exports = config;