import numpy as np
import gensim
from sklearn.preprocessing import OneHotEncoder

class PreTrainEmbedding():
    def __init__(self, file, embedding_size):
        self.embedding_size = embedding_size
        self.char_alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-,;.!?:\'"/\\|_@#$%^&*~`+=<>()[]{} '
        self.char_embedding_table = self.construct_char_embeddings()
        self.model = gensim.models.Word2Vec.load_word2vec_format(file, binary=True)

    def get_one_hot_encoding(self, target_classes):
        enc = OneHotEncoder()
        return enc.fit_transform(np.array(target_classes).reshape(-1,1)).toarray()

    def construct_char_embeddings(self):
        ascii_list = [ord(c) for c in self.char_alphabet]
        ascii_list.sort()
        encodings = self.get_one_hot_encoding(ascii_list)
        result = dict()
        for i, enc in enumerate(encodings):
            result[ascii_list[i]] = enc
        return result
    
    def get_embedding(self, word):
        try:
            result = self.model[word]
            return result
        except KeyError:
            #print 'Can not get embedding for ', word
            return None
    
    def get_glove_embedding(self, vectors_file='glove.6B.100d.txt'):
        with open(vectors_file, 'r') as f:
            vectors = {}
            for line in f:
                vals = line.rstrip().split(' ')
                vectors[vals[0]] = [float(x) for x in vals[1:]]

        vocab_size = len(vectors)
        words = vectors.keys()
        vocab = {w: idx for idx, w in enumerate(words)}
        ivocab = {idx: w for idx, w in enumerate(words)}

        vector_dim = len(vectors[ivocab[0]])
        W = np.zeros((vocab_size, vector_dim))
        for word, v in vectors.items():
            if word == '<unk>':
                continue
            W[vocab[word], :] = v
        return vocab, W
    
    def create_char_level_embeddings(self, sentence, max_doc_length):
        sent_embed = np.zeros((max_doc_length, self.embedding_size))
        idx = 0
        for c in sentence:
            try:
                sent_embed[idx, :] = self.char_embedding_table[ord(c)]
            except KeyError:
                pass
                continue
            idx = idx + 1
            if idx == max_doc_length:
                break
        return sent_embed
    
    def create_word_level_embeddings(self, sentence, max_doc_length):
        sent_embed = np.zeros((max_doc_length, self.embedding_size))
        if sentence is None:
            return sent_embed
        idx = 0
        for word in sentence.split():
            embedding = self.get_embedding(word)
            if embedding is not None:
                sent_embed[idx, :] = embedding
            idx = idx + 1
            if idx == max_doc_length:
                break
        return sent_embed
    
    