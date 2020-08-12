const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
var HtmlWebpackHarddiskPlugin = require('html-webpack-harddisk-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin')

let resourcesPath = path.resolve(__dirname, '../../../../client/build/processedResources/Js/main');

if (config.output)
  config.output.publicPath = '/app/build/'

config.resolve.modules.push(resourcesPath);
config.resolve.modules.push(path.resolve(__dirname, '../../../../build/js/node_modules'));

config.module.rules.push(
  {
    test: /\.md$/i, use: 'raw-loader'
  },
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
        options: {
          modules: 'global'
        }
      },
    ],
  }, {
    test: /\.(png|woff|woff2|eot|ttf|svg)(\?v=\d+\.\d+\.\d+)?$/,
    loader: [
      'url-loader?limit=100000'
    ]
  }
);
config.plugins.push(new MiniCssExtractPlugin({
    filename: 'styles.css'
  }),
  new HtmlWebpackPlugin({
    alwaysWriteToDisk: true,
    title: 'Coupling Dev Server',
    file: "index.html",
    template: path.resolve(resourcesPath, 'template.html')
  }),
  new HtmlWebpackHarddiskPlugin(),
  new FaviconsWebpackPlugin({
    logo: path.resolve(resourcesPath, 'images/tribes/no-tribe.png'),
    cache: true
  })
);

if (config.devServer) {
  config.devServer.port = 3001
  config.devServer.publicPath = '/app/build'
  config.devServer.hot = true
  config.devServer.historyApiFallback = {
    index: 'index.html'
  }
  let distributionPath = path.resolve(__dirname, '../../../../client/build/distributions');
  config.devServer.contentBase.push(distributionPath)
}
