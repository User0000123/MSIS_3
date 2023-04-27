public class TokenEl {
        public String token;
        public int count;

        public TokenEl(TToken tToken, int count){
            this.token = tToken.tokenValue();
            this.count = count;
        }

        public int getCount(){
            return count-1;
        }
        public String getToken(){
            return token;
        }
    }
