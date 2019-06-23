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
      "angular-mocks",
      "angular-route",
      "axios",
      "kotlin",
      "kotlin-logging",
      "kotlinx-serialization-runtime-js",
      "logging",
      "prefixfree",
      "klock",
      "d3-color",
      "d3-interpolate",
      "d3-selection",
      "date-fns",
      "ramda/es/map",
      "ramda/es/find",
      "ramda/es/merge",
      "ramda/es/mergeAll",
      "ramda/es/pipe",
      "ramda/es/flatten",
      "ramda/es/prop",
      "ramda/es/propEq",
      "ramda/es/eqBy",
      "ramda/es/differenceWith",
      "ramda/es/values",
      "ramda/es/filter",
      "ramda/es/unnest",
      "ramda/es/curry",
      "ramda/es/length",
      "ramda/es/__",
      "ramda/es/where",
      "ramda/es/contains",
      "ramda/es/union",
      "ramda/es/pluck",
      "react",
      "react-dom",
      "font-awesome/css/font-awesome.css",
      "date-fns/parse",
      "date-fns/distance_in_words",
      "fitty",
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