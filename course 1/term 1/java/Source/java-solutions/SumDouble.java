public class SumDouble {
    public static void main(String[] args) {
        Double sum = 0D;
        for(String arg : args) {
            StringBuilder num = new StringBuilder();
            for(Character i : arg.toCharArray()) {
                if(!Character.isWhitespace(i)) {
                    num.append(String.valueOf(i));
                } else {
                    if(!num.isEmpty()) {
                        sum += Double.parseDouble(num.toString());
                    }
                    num.setLength(0);
                }
            }
            if(!num.isEmpty()){
                sum += Double.parseDouble(num.toString());
            }
        }
        System.out.println(sum);
    }
}
