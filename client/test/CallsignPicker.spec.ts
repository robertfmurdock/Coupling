import CallSignPicker from "../app/wordpicker/CallSignPicker";

describe('The CallSign Picker', function () {

    it('will produce an adjective and a noun based on an email', function () {
      const callSignPicker = new CallSignPicker();
      const {adjective, noun} = callSignPicker.pick('myEmail@gmail.com');

      expect(adjective).toBeDefined();
      expect(noun).toBeDefined();
    });

});