var filters = angular.module("coupling.filters", []);
filters.filter('gravatarUrl', ['gravatarService', function (gravatarService) {
    return function (player, options) {
        if (player && player.imageURL) {
            return player.imageURL;
        } else {
            var email = player && player.email ? player.email : "";
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
                return gravatarService.url(tribe.email, options);
            }
        }
        return "/images/icons/tribes/no-tribe.png";
    }
}]);