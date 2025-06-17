from mrjob.job import MRJob
from mrjob.step import MRStep

class MRMatrixMultiply(MRJob):

    def configure_args(self):
        """Define los argumentos de línea de comandos para las dimensiones."""
        super(MRMatrixMultiply, self).configure_args()
        self.add_passthru_arg('--m', type=int, help='Número de filas en la matriz A')
        self.add_passthru_arg('--n', type=int, help='Columnas de A / Filas de B')
        self.add_passthru_arg('--p', type=int, help='Número de columnas en la matriz B')

    def mapper(self, _, line):
        """Fase de Mapeo."""
        matrix_id, row, col, val_str = line.split(',')
        row, col, val = int(row), int(col), float(val_str)

        if matrix_id == 'A':
            # Elemento A(i,k) -> emitir para cada columna j de B
            for j in range(self.options.p):
                yield (row, j), ('A', col, val)
        else: # matrix_id == 'B'
            # Elemento B(k,j) -> emitir para cada fila i de A
            for i in range(self.options.m):
                yield (i, col), ('B', row, val)

    def reducer(self, key, values):
        """Fase de Reducción."""
        row_a = {}
        col_b = {}

        for matrix_id, index, val in values:
            if matrix_id == 'A':
                # Almacena el valor de A(i,k) con k como clave
                row_a[index] = val
            else:
                # Almacena el valor de B(k,j) con k como clave
                col_b[index] = val

        # Calcular el producto punto para C(i,j)
        result = sum(row_a.get(k, 0) * col_b.get(k, 0) for k in range(self.options.n))

        if result != 0:
            yield key, result

    # Opcional: Combiner. Para este algoritmo es complejo y se omite por claridad.
    # El trabajo se puede definir en pasos, pero para este caso uno es suficiente.
    def steps(self):
        return [MRStep(mapper=self.mapper, reducer=self.reducer)]

if __name__ == '__main__':
    MRMatrixMultiply.run()