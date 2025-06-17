# Script de verificación (verification.py)
import numpy as np

# Usa las mismas dimensiones que en generate_input.py
M, N, P = 50, 100, 75

# Re-genera las mismas matrices si usamos una semilla fija (seed)
# o simplemente crea dos nuevas para una prueba conceptual.
np.random.seed(0) # Usar la misma semilla para reproducibilidad
matrix_a = np.random.rand(M, N)
matrix_b = np.random.rand(N, P)

# Calcular con NumPy
matrix_c = np.dot(matrix_a, matrix_b)

print("Resultado esperado para C[0,0] usando NumPy:")
print(matrix_c[0, 0])

# Ahora, busca el valor para [0,0] en tu `result_output.txt`
# y compáralo. Deberían ser prácticamente idénticos.