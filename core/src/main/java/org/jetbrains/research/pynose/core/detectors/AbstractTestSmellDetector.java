package org.jetbrains.research.pynose.core.detectors;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.pynose.core.PyNoseUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(AbstractTestSmellDetector.class);
    protected PyClass testCase;
    protected PyFunction currentMethod;

    public abstract void analyze();

    public abstract void reset();

    public abstract void reset(PyClass testCase);

    public String getSmellName() {
        var className = getClass().getName();
        return className.substring("pynose.".length(), className.length() - "TestSmellDetector".length());
    }

    public abstract boolean hasSmell();

    public String getSmellDetail() {
        return getSmellDetailJSON().toString();
    }

    public abstract JsonObject getSmellDetailJSON();

    protected JsonObject templateSmellDetailJSON() {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("name", this.getSmellName());
        jsonObject.addProperty("hasSmell", this.hasSmell());
        return jsonObject;
    }

    protected abstract static class MyPsiElementVisitor extends PsiElementVisitor {
        public void visitElement(@NotNull PsiElement element) {
            var interfaces = Arrays.stream(element.getClass().getInterfaces())
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

                var anInterface = interfaces.get(0);
                try {
                    var customVisitMethod = this.getClass().getMethod(
                            "visit" + anInterface.getSimpleName(),
                            anInterface
                    );
                    try {
                        customVisitMethod.invoke(this, element);
                        return;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        LOG.warn(PyNoseUtils.exceptionToString(e));
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }

            for (var child : element.getChildren()) {
                visitElement(child);
            }
        }
    }
}
