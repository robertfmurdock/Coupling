const webpack = require('webpack');
const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const buildPath = path.resolve(__dirname, 'build/lib/vendor');

const config = {
  mode: process.env.NODE_ENV,
  entry: {
    vendor: [
      "axios",
      "blueimp-md5",
      "core-js",
      "commonKt",
      "kotlin",
      "kotlin-extensions",
      "kotlin-react",
      "kotlin-react-dom",
      "kotlin-logging",
      "kotlin-css-js",
      "kotlin-styled",
      "styled-components",
      "inline-style-prefixer",
      "kotlinx-serialization-runtime-js",
      "logging",
      "prefixfree",
      "klock",
      "d3-color",
      "d3-interpolate",
      "d3-selection",
      "date-fns",
      "history",
      "react",
      "react-dom",
      "react-websocket",
      "react-router-dom",
      "react-dnd",
      "react-dnd-html5-backend",
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
    rules: [
      {
        test: /\.(sa|sc|c)ss$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
            options: {
              hmr: process.env.NODE_ENV === 'development',
            },
          },
          {
            loader: 'css-loader',
          },
          'sass-loader',
        ],
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
    new MiniCssExtractPlugin({
      filename: '[name]-styles.css'
    }),
    new webpack.DllPlugin({
      path: path.resolve(buildPath, '[name]-manifest.json'),
      name: '[name]_lib'
    }),
    new webpack.optimize.OccurrenceOrderPlugin()
  ]
};


module.exports = config;