var express = require('express');

var router = express.Router({mergeParams: true});
router.post('/spin', require('./spin'));
router.use('/history', require('./history'));
router.use('/players', require('./players'));
router.use('/pins', require('./pins'));
module.exports = router;