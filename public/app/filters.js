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

filters.filter('tribeImageUrl', function () {
    return function (tribe) {
        if (tribe && tribe.image) {
            return "/images/icons/tribes/" + tribe.image;
        } else {
            return "/images/icons/tribes/no-tribe.png";
        }
    }
});