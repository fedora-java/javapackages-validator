/*-
 * Copyright (c) 2012-2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.javadeptools.rpm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SymbolLookup;
import jdk.incubator.foreign.ValueLayout;

/**
 * @author Mikolaj Izdebski
 */
final class Rpm {

    static final int RPMRC_OK = 0;
    static final int RPMRC_NOTFOUND = 1;
    static final int RPMRC_FAIL = 2;
    static final int RPMRC_NOTTRUSTED = 3;
    static final int RPMRC_NOKEY = 4;

    static final int RPMVSF_NOHDRCHK = 1 << 0;
    static final int RPMVSF_NOSHA1HEADER = 1 << 8;
    static final int RPMVSF_NODSAHEADER = 1 << 10;
    static final int RPMVSF_NORSAHEADER = 1 << 11;
    static final int RPMVSF_NOMD5 = 1 << 17;
    static final int RPMVSF_NODSA = 1 << 18;
    static final int RPMVSF_NORSA = 1 << 19;

    static final int RPMTAG_NAME = 1000;
    static final int RPMTAG_VERSION = 1001;
    static final int RPMTAG_RELEASE = 1002;
    static final int RPMTAG_EPOCH = 1003;
    static final int RPMTAG_ARCH = 1022;
    static final int RPMTAG_SOURCERPM = 1044;
    static final int RPMTAG_BUILDARCHS = 1089;
    static final int RPMTAG_EXCLUSIVEARCH = 1061;
    static final int RPMTAG_PROVIDENAME = 1047;
    static final int RPMTAG_REQUIRENAME = 1049;
    static final int RPMTAG_CONFLICTNAME = 1054;
    static final int RPMTAG_OBSOLETENAME = 1090;
    static final int RPMTAG_SOURCEPACKAGE = 1106;
    static final int RPMTAG_PAYLOADCOMPRESSOR = 1125;
    static final int RPMTAG_PAYLOADFORMAT = 1124;
    static final int RPMTAG_ORDERNAME = 5035;
    static final int RPMTAG_RECOMMENDNAME = 5046;
    static final int RPMTAG_SUGGESTNAME = 5049;
    static final int RPMTAG_SUPPLEMENTNAME = 5052;
    static final int RPMTAG_ENHANCENAME = 5055;

    static final int HEADERGET_MINMEM = 1 << 0;

    static final int RPMDBI_INSTFILENAMES = 5040;

    private static class Layouts {
        private static ValueLayout findLayout(int bitSize) {
            for (ValueLayout layout : Arrays.asList(CLinker.C_CHAR, CLinker.C_SHORT,
                    CLinker.C_INT, CLinker.C_LONG, CLinker.C_LONG_LONG)) {
                if (layout.bitSize() == bitSize) {
                    return layout;
                }
            }

            return null;
        }

        // TODO cleaner solution possible in JDKs > 17
        // static final ValueLayout int32_t = ValueLayout.JAVA_INT;
        // static final ValueLayout int64_t = ValueLayout.JAVA_LONG;
        static final ValueLayout int32_t = findLayout(32);
        static final ValueLayout int64_t = findLayout(64);

        static final ValueLayout size_t = CLinker.C_LONG_LONG;
    }

    private static final MemoryAddress toCStringAddress(String string, ResourceScope scope) {
        if (string == null) {
            return MemoryAddress.NULL;
        } else {
            return CLinker.toCString(string, scope).address();
        }
    }

    private static void loadLibrary(String name) {
        for (String libdirname : System.getProperty("java.library.path").split(":")) {
            try {
                var libpath = Paths.get(libdirname);
                if (Files.isDirectory(libpath)) {
                    var optLib = Files.find(libpath, Integer.MAX_VALUE, (path, attributes) ->
                        attributes.isRegularFile() && path.getFileName().toString().startsWith("lib" + name + ".so")
                    ).findFirst();
                    if (optLib.isPresent()) {
                        System.load(optLib.get().toString());
                        return;
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        throw new UnsatisfiedLinkError("Library " + name + " not found in " + System.getProperty("java.library.path"));
    }

    private static class Library {
        private final SymbolLookup lookup;
        private final CLinker clinker;

        Library(String... libraries) {
            /*
             * Libraries and fields have to be initialized in this parent
             * constructor before inherited final method handles are initialized
             */
            lookup = SymbolLookup.loaderLookup();
            clinker = CLinker.getInstance();

            for (String library : libraries) {
                loadLibrary(library);
            }
        }

        final MethodHandle downcallHandle(String symbol, MethodType type, FunctionDescriptor descriptor) {
            return clinker.downcallHandle(lookup.lookup(symbol).get(), type, descriptor);
        }
    }

    private static class RpmLib extends Library {
        RpmLib() {
            super("rpm");
        }

        final MethodHandle rpmtsCreate = downcallHandle("rpmtsCreate",
                MethodType.methodType(MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER));
        final MethodHandle rpmtsFree = downcallHandle("rpmtsFree",
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(CLinker.C_POINTER));
        final MethodHandle rpmtsSetVSFlags = downcallHandle("rpmtsSetVSFlags",
                MethodType.methodType(int.class, MemoryAddress.class, int.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT));
        final MethodHandle rpmtsSetRootDir = downcallHandle("rpmtsSetRootDir",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER));
        final MethodHandle rpmtsInitIterator = downcallHandle("rpmtsInitIterator",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, int.class, MemoryAddress.class, long.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER, Layouts.int32_t, CLinker.C_POINTER, Layouts.size_t));
        final MethodHandle rpmdbFreeIterator = downcallHandle("rpmdbFreeIterator",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER));
        final MethodHandle rpmdbNextIterator = downcallHandle("rpmdbNextIterator",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER));
        final MethodHandle rpmReadPackageFile = downcallHandle("rpmReadPackageFile",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER));
        final MethodHandle rpmReadConfigFiles = downcallHandle("rpmReadConfigFiles",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER));
        final MethodHandle headerFree = downcallHandle("headerFree",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER));
        final MethodHandle headerGet = downcallHandle("headerGet",
                MethodType.methodType(int.class, MemoryAddress.class, int.class, MemoryAddress.class, int.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT));
        final MethodHandle headerGetString = downcallHandle("headerGetString",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, int.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_INT));
        final MethodHandle headerGetNumber = downcallHandle("headerGetNumber",
                MethodType.methodType(long.class, MemoryAddress.class, int.class),
                FunctionDescriptor.of(Layouts.int64_t, CLinker.C_POINTER, CLinker.C_INT));
        final MethodHandle rpmtdNew = downcallHandle("rpmtdNew",
                MethodType.methodType(MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER));
        final MethodHandle rpmtdFree = downcallHandle("rpmtdFree",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER));
        final MethodHandle rpmtdFreeData = downcallHandle("rpmtdFreeData",
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(CLinker.C_POINTER));
        final MethodHandle rpmtdCount = downcallHandle("rpmtdCount",
                MethodType.methodType(int.class, MemoryAddress.class),
                FunctionDescriptor.of(Layouts.int32_t, CLinker.C_POINTER));
        final MethodHandle rpmtdNext = downcallHandle("rpmtdNext",
                MethodType.methodType(int.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER));
        final MethodHandle rpmtdGetString = downcallHandle("rpmtdGetString",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER));
    }

    private static class RpmIO extends Library {
        RpmIO() {
            super("rpmio");
        }

        final MethodHandle Fopen = downcallHandle("Fopen",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER));
        final MethodHandle Fclose = downcallHandle("Fclose",
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(CLinker.C_POINTER));
        final MethodHandle Ftell = downcallHandle("Ftell",
                MethodType.methodType(long.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_LONG, CLinker.C_POINTER));
        final MethodHandle Ferror = downcallHandle("Ferror",
                MethodType.methodType(int.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER));
        final MethodHandle Fstrerror = downcallHandle("Fstrerror",
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER));
    }

    private static class Lazy {
        static final RpmLib RPM = new RpmLib();
    }

    private static class LazyIO {
        static final RpmIO RPMIO = new RpmIO();
    }

    static final MemoryAddress rpmtsCreate() {
        try {
            return (MemoryAddress) Lazy.RPM.rpmtsCreate.invokeExact();
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final void rpmtsFree(MemoryAddress ts) {
        try {
            Lazy.RPM.rpmtsFree.invokeExact(ts);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final int rpmtsSetVSFlags(MemoryAddress ts, int flags) {
        try {
            return (int) Lazy.RPM.rpmtsSetVSFlags.invokeExact(ts, flags);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final int rpmtsSetRootDir(MemoryAddress ts, String rootDir) {
        try (var rootDirScope = ResourceScope.newConfinedScope()) {
            return (int) Lazy.RPM.rpmtsSetRootDir.invokeExact(
                    ts,
                    toCStringAddress(rootDir, rootDirScope));
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final MemoryAddress rpmtsInitIterator(MemoryAddress ts, int rpmtag, String keyp, long keylen) {
        try (var keypScope = ResourceScope.newConfinedScope()) {
            return (MemoryAddress) Lazy.RPM.rpmtsInitIterator.invokeExact(
                    ts,
                    rpmtag,
                    toCStringAddress(keyp, keypScope),
                    keylen);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final MemoryAddress rpmdbFreeIterator(MemoryAddress mi) {
        try {
            return (MemoryAddress) Lazy.RPM.rpmdbFreeIterator.invokeExact(mi);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final MemoryAddress rpmdbNextIterator(MemoryAddress mi) {
        try {
            return (MemoryAddress) Lazy.RPM.rpmdbNextIterator.invokeExact(mi);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final int rpmReadPackageFile(MemoryAddress ts, MemoryAddress fd, MemoryAddress fn, MemoryAddress hdr) {
        try {
            return (int) Lazy.RPM.rpmReadPackageFile.invokeExact(ts, fd, fn, hdr);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final int rpmReadConfigFiles(String file, String target) {
        try (var fileScope = ResourceScope.newConfinedScope();
             var targetScope = ResourceScope.newConfinedScope()) {
            return (int) Lazy.RPM.rpmReadConfigFiles.invokeExact(
                    toCStringAddress(file, fileScope),
                    toCStringAddress(target, targetScope));
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final MemoryAddress headerFree(MemoryAddress hdr) {
        try {
            return (MemoryAddress) Lazy.RPM.headerFree.invokeExact(hdr);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final int headerGet(MemoryAddress hdr, int tag, MemoryAddress td, int flags) {
        try {
            return (int) Lazy.RPM.headerGet.invokeExact(hdr, tag, td, flags);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final String headerGetString(MemoryAddress hdr, int tag) {
        try {
            var cstring = (MemoryAddress) Lazy.RPM.headerGetString.invokeExact(hdr, tag);
            if (cstring.equals(MemoryAddress.NULL)) {
                return null;
            } else {
                return CLinker.toJavaString(cstring);
            }
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final long headerGetNumber(MemoryAddress hdr, int tag) {
        try {
            return (long) Lazy.RPM.headerGetNumber.invokeExact(hdr, tag);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final MemoryAddress rpmtdNew() {
        try {
            return (MemoryAddress) Lazy.RPM.rpmtdNew.invokeExact();
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final MemoryAddress rpmtdFree(MemoryAddress td) {
        try {
            return (MemoryAddress) Lazy.RPM.rpmtdFree.invokeExact(td);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final void rpmtdFreeData(MemoryAddress td) {
        try {
            Lazy.RPM.rpmtdFreeData.invokeExact(td);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final int rpmtdCount(MemoryAddress td) {
        try {
            return (int) Lazy.RPM.rpmtdCount.invokeExact(td);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final int rpmtdNext(MemoryAddress td) {
        try {
            return (int) Lazy.RPM.rpmtdNext.invokeExact(td);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final String rpmtdGetString(MemoryAddress td) {
        try {
            return CLinker.toJavaString((MemoryAddress) Lazy.RPM.rpmtdGetString.invokeExact(td));
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final MemoryAddress Fopen(String filepath, String mode) {
        try (var filepathScope = ResourceScope.newConfinedScope();
             var modeScope = ResourceScope.newConfinedScope()) {
            return (MemoryAddress) LazyIO.RPMIO.Fopen.invokeExact(
                    toCStringAddress(filepath, filepathScope),
                    toCStringAddress(mode, modeScope));
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final void Fclose(MemoryAddress ts) {
        try {
            LazyIO.RPMIO.Fclose.invokeExact(ts);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final long Ftell(MemoryAddress fd) {
        try {
            return (long) LazyIO.RPMIO.Ftell.invokeExact(fd);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final int Ferror(MemoryAddress fd) {
        try {
            return (int) LazyIO.RPMIO.Ferror.invokeExact(fd);
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    static final String Fstrerror(MemoryAddress fd) {
        try {
            return CLinker.toJavaString((MemoryAddress) LazyIO.RPMIO.Fstrerror.invokeExact(fd));
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }
}
