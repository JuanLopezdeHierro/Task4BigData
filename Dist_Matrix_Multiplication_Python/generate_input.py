import numpy as np

# Dimensiones de las matrices (M x N) y (N x P)
M = 50
N = 100
P = 75


def generate_matrix_file(filename="matrix_input.txt"):
    """Genera un archivo de texto con dos matrices aleatorias."""
    print(f"Generando matriz A ({M}x{N}) y B ({N}x{P})...")
    matrix_a = np.random.rand(M, N)
    matrix_b = np.random.rand(N, P)

    with open(filename, "w") as f:
        # Escribir matriz A
        for i in range(M):
            for k in range(N):
                # Formato: id_matriz, fila, columna, valor
                f.write(f"A,{i},{k},{matrix_a[i, k]}\n")

        # Escribir matriz B
        for k in range(N):
            for j in range(P):
                f.write(f"B,{k},{j},{matrix_b[k, j]}\n")

    print(f"Archivo de entrada '{filename}' creado con éxito.")
    print(f"Dimensiones para el job: --m {M} --n {N} --p {P}")


if __name__ == "__main__":
    generate_matrix_file()