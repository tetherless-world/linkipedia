import tensorflow as tf
from numpy import squeeze

class ConvNet():
    def __init__(self, embed_dim=300,
                 feature_maps=128,
                 kernels=[1,2,3,4,5,6,7]):
        """Initialize the parameters for TDNN
        
        Args:
          embed_dim: the dimensionality of the inputs
          feature_maps: list of feature maps (for each kernel width)
          kernels: list of # of kernels (width)
        """
        self.embed_dim = embed_dim
        self.feature_maps = feature_maps
        self.kernels = kernels

    def conv_net(self, input_):
        
        # [batch_size x seq_length x embed_dim x 1]ccaA
        input_ = tf.expand_dims(input_, -1)
        
        layers = []
        for idx, kernel_dim in enumerate(self.kernels):
            reduced_length = input_.get_shape()[1] - kernel_dim + 1
            #print 'hello ',  reduced_length
            
            W = tf.Variable(tf.truncated_normal(
                [self.kernels[idx], self.embed_dim, 1, self.feature_maps], stddev=0.1), name="W")
            # [batch_size x seq_length x embed_dim x feature_map_dim]
            conv = tf.nn.conv2d(input_, 
                                W, 
                                strides=[1, 1, 1, 1], 
                                padding="VALID", 
                                name="kernel%d" % idx)
        
            # [batch_size x 1 x 1 x feature_map_dim]
            pool = tf.nn.max_pool(tf.tanh(conv), [1, reduced_length, 1, 1], [1, 1, 1, 1], 'VALID')
            layers.append(pool)
        
        if len(self.kernels) > 1:
            self.output = tf.concat(1, layers)
        else:
            self.output = layers[0]

        if self.feature_maps > 1:
            self.output = tf.concat(3, self.output)

        num_filters_total = self.feature_maps * len(self.kernels)
        return tf.reshape(self.output, [-1, num_filters_total])
            
            