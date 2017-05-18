
import numpy as np
import gensim
from sklearn.preprocessing import OneHotEncoder
import string


class PreTrainEmbedding:
    def __init__(self, word2vec_file, embedding_size, binary=False):
        self.embedding_size = embedding_size
        self.char_alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz' \
                             '0123456789-,;.!?:\'"/\\|_@#$%^&*~`+=<>()[]{} '
        self.model = gensim.models.Word2Vec.load_word2vec_format(word2vec_file, binary=binary)

    def construct_char_embeddings(self):
        ascii_list = [ord(c) for c in self.char_alphabet]
        ascii_list.sort()
        encodings = self.get_one_hot_encoding(ascii_list)
        result = dict()
        for i, enc in enumerate(encodings):
            result[ascii_list[i]] = enc
        return result

    @staticmethod
    def get_one_hot_encoding(target_classes):
        return OneHotEncoder().fit_transform(np.array(target_classes).reshape(-1, 1)).toarray()
    
    def get_embedding(self, word):
        try:
            result = self.model[word]
            return result
        except KeyError:
            # print 'Can not get embedding for ', word
            return self.get_embedding_from_char(word)

    def get_embedding_from_char(self, word):
        word_embed = np.zeros(self.embedding_size)
        for c in word:
            try:
                word_embed[ord(c) % self.embedding_size] += 0.1
            except:
                continue
        return word_embed

    def create_char_level_embeddings(self, sentence, max_len):
        sent_embed = np.zeros((max_len, self.embedding_size))
        i = 0
        for c in sentence:
            try:
                sent_embed[i, ord(c) % self.embedding_size] = 0.1
            except:
                continue
            i = i + 1
            if i == max_len:
                break
        return sent_embed
    
    def create_word_level_embeddings(self, sentence, max_len):
        sent_embed = np.zeros((max_len, self.embedding_size))
        if sentence is None:
            return sent_embed
        sentence = PreTrainEmbedding.remove_punctuation(sentence)
        i = 0
        for word in sentence.split():
            embedding = self.get_embedding(word)
            if embedding is not None:
                sent_embed[i, :] = embedding
            i = i + 1
            if i == max_len:
                break
        return sent_embed

    @staticmethod
    def remove_punctuation(sentence):
        if isinstance(sentence, tuple):
            sentence = sentence[0]
        for p in string.punctuation:
            sentence = sentence.replace(p, ' ')
        return sentence



