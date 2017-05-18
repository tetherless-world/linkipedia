
from BiLSTM import BiLSTM
from data_helper import DataHelper


class BiLSTMAnnotator:
    def __init__(self):
        pass

    @staticmethod
    def main():
        if raw_input("%s (y/N) " % 'Read data set from file?').lower() == 'y':
            if raw_input("%s (y/N) " % 'Resplit training and test data?').lower() == 'y':
                manu_anno = DataHelper.get_data_from_file('data.json')
                train_anno, test_anno = DataHelper.split_dataset(manu_anno, 0.7)
                DataHelper.save_data(train_anno, 'train_data.json')
                DataHelper.save_data(test_anno, 'test_data.json')
                print 'Finish spliting training and test data!'
            else:
                train_anno = DataHelper.get_data_from_file('train_data.json')
                test_anno = DataHelper.get_data_from_file('test_data.json')
                print 'Finish reading training and test data!'

            retrain = raw_input("%s (y/N) " % 'Do you want to retrain the model?').lower() == 'y'

            nt_file = 'dataone-index/NTriple/d1-ESCO-imported-2.nt'
            mt_from_onto = DataHelper.get_all_measurement_types(nt_file)
            train_pairs = DataHelper.get_pairs(mt_from_onto, train_anno, 2)
            test_pairs = DataHelper.get_pairs(mt_from_onto, test_anno)

            bilstm = BiLSTM(retrain=retrain, train_data=train_pairs, test_data=test_pairs, mt_table=mt_from_onto,
                            epochs=20, embedding_size=200, max_doc_length=50)
            bilstm.define()
            bilstm.train()
        else:
            manu_anno = DataHelper.read_and_save_data('data.json')
            train_anno, test_anno = DataHelper.split_dataset(manu_anno, 0.7)
            DataHelper.save_data(train_anno, 'train_data.json')
            DataHelper.save_data(test_anno, 'test_data.json')
            print 'Finish spliting training and test data!'

if __name__ == '__main__':
    BiLSTMAnnotator.main()

