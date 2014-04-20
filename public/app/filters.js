var filters = angular.module("coupling.filters", []);
filters.filter('gravatarUrl', function (gravatarService) {
    return function (player, options) {
        if (player && player.image) {
            return "/images/icons/" + player.image;
        } else {
            var email = player && player.email ? player.email : "";
            return gravatarService.url(email, options);
        }
    }
});