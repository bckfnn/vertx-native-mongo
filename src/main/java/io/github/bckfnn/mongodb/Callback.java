package io.github.bckfnn.mongodb;

public interface Callback<T> {
    public void handle(T result);
    public void error(Throwable error);
    public void end();
    
    public abstract static class Default<T> implements Callback<T> {
        Callback<?> delegate;
        
        public Default(Callback<?> delegate) {
            this.delegate = delegate;
        }
        
        public void error(Throwable error) {
            delegate.error(error);
        }
        
        public void end() {
            delegate.end();
        }
        
    }
}
