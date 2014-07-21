var filters = angular.module("coupling.filters", []);
filters.filter('gravatarUrl', function (gravatarService) {
    return function (player, options) {
        if (player && player.image) {
            return "/images/icons/players/" + player.image;
        } else {
            var email = player && player.email ? player.email : "";
            return gravatarService.url(email, options);
        }
    }
});

filters.filter('tribeImageUrl', function (gravatarService) {
    return function (tribe) {
        if (tribe) {
            if (tribe.imageURL) {
                return tribe.imageURL;
            } else if (tribe.image) {
                return "/images/icons/tribes/" + tribe.image;
            } else if (tribe.email) {
                return gravatarService.url(tribe.email, {size: 75});
            }
        }
        return "/images/icons/tribes/no-tribe.png";
    }
});