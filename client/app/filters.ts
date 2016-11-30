

var filters = angular.module("coupling.filters", []);
filters.filter('gravatarUrl', ['gravatarService', function (gravatarService) {
    return function (player, options) {
        if (player && player.imageURL) {
            return player.imageURL;
        } else {
            options['default'] = "retro";
            var email = "";
            if (player) {
                email = player.email ? player.email : player.name;
            }
            return gravatarService.url(email, options);
        }
    }
}]);

filters.filter('tribeImageUrl', ['gravatarService', function (gravatarService) {
    return function (tribe, options) {
        if (tribe) {
            if (tribe.imageURL) {
                return tribe.imageURL;
            } else if (tribe.email) {
                options['default'] = "identicon";
                return gravatarService.url(tribe.email, options);
            }
        }
        return "/images/icons/tribes/no-tribe.png";
    }
}]);