var filters = angular.module("coupling.filters", []);
filters.filter('gravatarUrl', function (gravatarService) {
    return function (email, options) {
        email = email ? email : "";
        return gravatarService.url(email, options);
    }
});