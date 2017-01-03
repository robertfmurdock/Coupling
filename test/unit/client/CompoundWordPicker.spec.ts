import CompoundWordPicker from "../../../client/app/CompoundWordPicker";
import WordPicker from "../../../client/app/WordPicker";

describe('CompoundWordPicker', function () {

    function fakeWordPicker(result: string): WordPicker {
        return {
            choose: jasmine.createSpy('choose').and.returnValue(result)
        }
    }

    it('will take an array of word pickers and use them to pick multiple words based on input', function () {
        const compoundWordPicker = new CompoundWordPicker([
            fakeWordPicker('a1'),
            fakeWordPicker('b2'),
            fakeWordPicker('c3')
        ]);

        const value = 'input string value';
        const result = compoundWordPicker.choose(value);

        expect(result).toEqual(['a1', 'b2', 'c3']);
        compoundWordPicker.pickers.forEach(
            spyPicker => expect(spyPicker.choose).toHaveBeenCalledWith(value)
        );
    });
});