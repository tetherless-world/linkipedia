package aml.linkipedia;

 public class FreqValues{
	 
		int freq1;
		int freq2;
		
		FreqValues(int val1, int val2){
			this.freq1 = val1;
			this.freq2 = val2;
		}
		
		public void updateValues(int val1, int val2)
		{
			this.freq1 = val1;
			this.freq2 = val2;
		}
		
 }
