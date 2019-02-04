const webpack = require('webpack');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const buildPath = path.resolve(__dirname, 'build/lib/vendor');

const config = {
  mode: process.env.NODE_ENV,
  entry: {
    vendor: [
      "angular",
      "angular-animate",
      "angular-gravatar",
      "angular-mocks",
      "angular-native-dragdrop",
      "angular-resource",
      "angular-route",
      "angular-websocket",
      "axios",
      "kotlin",
      "klock",
      "d3-color",
      "d3-interpolate",
      "d3-selection",
      "ramda/src/map",
      "ramda/src/find",
      "ramda/src/merge",
      "ramda/src/mergeAll",
      "ramda/src/pipe",
      "ramda/src/flatten",
      "ramda/src/prop",
      "ramda/src/propEq",
      "ramda/src/eqBy",
      "ramda/src/differenceWith",
      "ramda/src/values",
      "ramda/src/filter",
      "ramda/src/unnest",
      "ramda/src/curry",
      "ramda/src/length",
      "ramda/src/__",
      "ramda/src/where",
      "ramda/src/contains",
      "ramda/src/union",
      "ramda/src/pluck",
      "font-awesome/css/font-awesome.css",
      "date-fns/parse",
      "date-fns/distance_in_words",
    ]
  },
  output: {
    path: buildPath,
    filename: '[name].js',
    library: "[name]_lib"
  },
  module: {
    noParse: /ws/,
    rules: [
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
  resolve: {
    modules: [
      path.resolve(__dirname, 'build/kotlin-js-min/main'),
      path.resolve(__dirname, 'node_modules')
    ]
  },
  externals: {
    ws: {}
  },
  plugins: [
    new ExtractTextPlugin({filename: '[name]-styles.css', allChunks: true}),
    new webpack.DllPlugin({
      path: path.resolve(buildPath, '[name]-manifest.json'),
      name: '[name]_lib'
    }),
    new webpack.optimize.OccurrenceOrderPlugin()
  ]
};


module.exports = config;