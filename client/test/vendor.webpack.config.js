const webpack = require('webpack');
const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const buildPath = path.resolve(__dirname, '../build/lib/test-vendor');

const config = {
  mode: process.env.NODE_ENV,
  entry: {
    testVendor: [
      "Coupling-logging",
      "Coupling-action",
      "axios",
      "blueimp-md5",
      "fitty",
      "prefixfree",
      "d3-color",
      "d3-interpolate",
      "d3-selection",
      "date-fns/formatDistance",
      "react-websocket",
      "react-dnd",
      "react-dnd-html5-backend",
      "font-awesome/css/font-awesome.css",
      "kotlin",
      "kotlin-styled",
      "kotlin-react",
      "kotlin-react-dom",
      "kotlin-react-router-dom",
      "klock-root-klock",
      "kotlin-css-js",
      "kotlinx-html-js",
      "kotlinx-serialization-kotlinx-serialization-runtime",
      "kotlinx-coroutines-core",
      "enzyme",
      "enzyme-adapter-react-16"
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
      path.resolve(__dirname, '../build/node_modules_imported'),
      path.resolve(__dirname, '../node_modules')
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