public class LinearRegression {
    private double[] theta; // Coefficients for the linear regression model

    public LinearRegression(int numFeatures) {
        theta = new double[numFeatures + 1]; // One extra for the bias term
    }

    // Hypothesis function
    private double hypothesis(double[] x) {
        double result = theta[0]; // Bias term

        for (int i = 1; i < theta.length; i++) {
            result += theta[i] * x[i - 1]; // Multiply each feature with its corresponding coefficient
        }

        return result;
    }

    // Cost function
    private double costFunction(double[][] X, double[] Y) {
        int m = X.length; // Number of training examples
        double sum = 0;

        for (int i = 0; i < m; i++) {
            double h = hypothesis(X[i]);
            sum += Math.pow((h - Y[i]), 2);
        }

        return sum / (2 * m);
    }

    // Gradient descent algorithm
    public void gradientDescent(double[][] X, double[] Y, double learningRate, int iterations) {
        int m = X.length; // Number of training examples
        int n = X[0].length; // Number of features

        for (int iter = 0; iter < iterations; iter++) {
            double[] newTheta = new double[n + 1];

            for (int j = 0; j < n; j++) {
                double gradient = 0;

                for (int i = 0; i < m; i++) {
                    double h = hypothesis(X[i]);
                    gradient += (h - Y[i]) * (j == 0 ? 1 : X[i][j - 1]); // Update for each feature
                }

                newTheta[j] = theta[j] - (learningRate / m) * gradient;
            }

            theta = newTheta;
        }
    }

    // Predict the output for new input
    public double predict(double[] x) {
        return hypothesis(x);
    }

    public static void main(String[] args) {
        // Example usage
        double[][] X = {{1, 1}, {1, 2}, {1, 3}}; // Features (including bias term)
        double[] Y = {2, 4, 5}; // Corresponding output values

        int numFeatures = X[0].length - 1; // Exclude bias term
        LinearRegression model = new LinearRegression(numFeatures);

        double learningRate = 0.01;
        int iterations = 1000;

        model.gradientDescent(X, Y, learningRate, iterations);

        // Predict for new input
        double[] newX = {1, 4}; // New feature values (including bias term)
        double prediction = model.predict(newX);

        System.out.println("Predicted value: " + prediction);
    }
}
