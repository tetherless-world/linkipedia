import pstats
import sys
p = pstats.Stats(sys.argv[1])
p.strip_dirs().sort_stats(sys.argv[2]).print_stats(sys.argv[3])
