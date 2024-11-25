public class Sum {
    public static void main(String[] args) {
        int sum = 0;
        for(String arg : args) {
            StringBuilder num = new StringBuilder();
            for(Character i : arg.toCharArray()) {
                if(!Character.isWhitespace(i)) {
                    num.append(String.valueOf(i));
                } else {
                    if(!num.isEmpty()) {
                        sum += Integer.parseInt(num.toString());
                    }
                    num.setLength(0);
                }
            }
            if(!num.isEmpty()){
                sum += Integer.parseInt(num.toString());
            }
        }
        System.out.println(sum);
    }
}