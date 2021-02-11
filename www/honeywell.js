var exec = require('cordova/exec');

var honeywell = {
    listenForScans: function(success, failure) {
        return exec(success, failure, "HoneywellScannerPlugin", "listenForScans", []);
    },
    manualMode: function(success, failure) {
        return exec(success, failure, "HoneywellScannerPlugin", "manualMode", []);
    },
    autoMode: function(success, failure) {
        return exec(success, failure, "HoneywellScannerPlugin", "autoMode", []);
    },
    startReading: function(success, failure) {
        return exec(success, failure, "HoneywellScannerPlugin", "startReading", []);
    },
    stopReading: function(success, failure) {
        return exec(success, failure, "HoneywellScannerPlugin", "stopReading", []);
    }

};

module.exports = honeywell;

