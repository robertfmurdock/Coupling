import RandomWordPicker from "../../../client/app/RandomWordPicker";

describe('RandomWordPicker', function () {

    describe('chooses the name using the value of given string ', function () {

        it('consistently', function () {
            const array = ['option1', 'option2', 'option3'];
            const assigner = new RandomWordPicker(array);
            const callSign1 = assigner.choose('emailjones@email.edu');
            const callSign2 = assigner.choose('emailjones@email.edu');
            expect(callSign2).toBe(callSign1);
        });

        it('with simple number input strings', function () {
            const array = ['option1', 'option2', 'option3'];
            const assigner = new RandomWordPicker(array);
            expect('option1').toBe(assigner.choose('0'));
            expect('option2').toBe(assigner.choose('1'));
            expect('option3').toBe(assigner.choose('2'));
        });

        it('even if value is greater than number of options', function () {
            const array = ['option1', 'option2', 'option3'];
            const assigner = new RandomWordPicker(array);
            expect('option2').toBe(assigner.choose('1'));
            expect('option3').toBe(assigner.choose('2'));
            expect('option1').toBe(assigner.choose('3'));
        });

        it('even if value is greater than number of options', function () {
            const array = ['option1', 'option2', 'option3'];
            const assigner = new RandomWordPicker(array);
            expect('option2').toBe(assigner.choose('1'));
            expect('option3').toBe(assigner.choose('2'));
            expect('option1').toBe(assigner.choose('3'));
        });

        it('even if value is non-numeric', function () {
            const array = [
                'option0',
                'option1',
                'option2',
                'option3',
                'option4',
                'option5',
                'option6',
                'option7',
                'option8',
                'option9',
            ];
            const assigner = new RandomWordPicker(array);
            expect('option7').toBe(assigner.choose('a'));
            expect('option8').toBe(assigner.choose('b'));
            expect('option9').toBe(assigner.choose('c'));
        });

        it('even if value has more than one numeric character', function () {
            const array = [
                'option0',
                'option1',
                'option2',
            ];
            const assigner = new RandomWordPicker(array);
            expect('option2').toBe(assigner.choose('11'));
        });

        it('even if value has more than one non-numeric character', function () {
            const array = [
                'option0',
                'option1',
                'option2',
            ];
            const assigner = new RandomWordPicker(array);
            expect('option2').toBe(assigner.choose('aa'));
            expect('option1').toBe(assigner.choose('bbc'));
            expect('option0').toBe(assigner.choose('Rob'));
        });

    });

});