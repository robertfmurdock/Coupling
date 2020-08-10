const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

let resourcesPath = path.resolve(__dirname, '../../../../client/build/processedResources/Js/main');

config.resolve.modules.push(resourcesPath);
config.resolve.extensions = ['.js'];

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
  filename: './styles.css'
}));

if (config.devServer) {
  config.devServer.port = 3001
  config.devServer.publicPath = '/app/build'
  config.devServer.hot = true
  config.devServer.historyApiFallback = {
    index: 'index.html'
  }
}
