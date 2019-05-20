const filters = angular.module("coupling.filters", []);

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