package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractTestSmellDetector {
    public abstract void analyze();
    public abstract void reset();
    public abstract void reset(PyClass testCase);
    public abstract String getSmellName();
    public abstract String getSmellDetail();

    protected PyClass testCase;
    protected PyFunction currentMethod;

    private static final Logger LOG = Logger.getInstance(AbstractTestSmellDetector.class);

    abstract static class MyPsiElementVisitor extends PsiElementVisitor {
        public void visitElement(@NotNull PsiElement element) {
            List<Class<?>> interfaces = Arrays.stream(element.getClass().getInterfaces())
                    .filter(i -> i.getName().startsWith("com.jetbrains.python.psi"))
                    .collect(Collectors.toList());

            if (interfaces.isEmpty()) {
                LOG.warn(element.getClass().getName() + " has no interface implemented");
            } else {

                // Assumption: assuming all Python psi implementations have only one interface from PSI module
                if (interfaces.size() > 1) {
                    LOG.warn(element.getClass().getName() +
                            " implements multiple interfaces from psi module: " +
                            interfaces.stream().map(Class::getName).collect(Collectors.joining(","))
                    );
                }

                Class<?> anInterface = interfaces.get(0);
                try {
                    // TODO: maybe scanning child class first to get a list of methods is faster
                    //  than going through all python psi interfaces
                    Method customVisitMethod = this.getClass().getMethod(
                            "visit" + anInterface.getSimpleName(),
                            anInterface
                    );
                    try {
                        customVisitMethod.invoke(this, element);
                        return;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        LOG.warn(Util.exceptionToString(e));
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }

            for (PsiElement child : element.getChildren()) {
                visitElement(child);
            }
        }
    }
}
